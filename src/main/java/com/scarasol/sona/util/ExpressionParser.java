package com.scarasol.sona.util;

import com.scarasol.sona.SonaMod;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Scarasol
 */
public class ExpressionParser {

    // 严格的数字格式正则：必须有整数部分，小数部分可选（无多个小数点）
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");

    public static double eval(String expr) {
        try {
            // 预处理：去除全空格场景
            if (expr == null || expr.trim().isEmpty()) {
                throw new RuntimeException("Empty expression");
            }
            List<String> rpn = toRPN(expr);
            return evalRPN(rpn);
        } catch (Exception e) {
            // 区分解析错误和计算错误，便于调试
            SonaMod.LOGGER.error("Expression Error: " + e.getMessage());
            return Double.NaN;
        }
    }

    // Shunting Yard算法：中缀表达式 -> 后缀表达式（RPN）
    private static List<String> toRPN(String expr) {
        List<String> output = new ArrayList<>();
        Deque<Character> ops = new ArrayDeque<>();
        int exprLen = expr.length();
        int i = 0;

        while (i < exprLen) {
            char ch = expr.charAt(i);

            // 1. 处理空格
            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }

            // 2. 处理数字（严格校验格式）
            if (Character.isDigit(ch) || ch == '.') {
                int numStartIdx = i; // 记录数字起始位置（用于异常提示）
                StringBuilder numBuilder = new StringBuilder();
                boolean hasDot = false;

                while (i < exprLen) {
                    char currChar = expr.charAt(i);
                    if (Character.isDigit(currChar)) {
                        numBuilder.append(currChar);
                    } else if (currChar == '.') {
                        if (hasDot) {
                            throw new RuntimeException(String.format(
                                    "Invalid number: multiple dots (start at position %d, char '%c' at %d)",
                                    numStartIdx, currChar, i));
                        }
                        hasDot = true;
                        numBuilder.append(currChar);
                    } else {
                        break; // 退出数字解析
                    }
                    i++;
                }

                // 严格校验数字格式（拒绝 "."、"123." 等不规范格式）
                String numStr = numBuilder.toString();
                if (!NUMBER_PATTERN.matcher(numStr).matches()) {
                    throw new RuntimeException(String.format(
                            "Invalid number format: '%s' (start at position %d)",
                            numStr, numStartIdx));
                }
                output.add(numStr);
                continue;
            }

            // 3. 处理括号
            if (ch == '(') {
                ops.push(ch);
                i++;
            } else if (ch == ')') {
                // 弹出运算符直到左括号
                while (!ops.isEmpty() && ops.peek() != '(') {
                    output.add(String.valueOf(ops.pop()));
                }
                // 检查括号匹配
                if (ops.isEmpty()) {
                    throw new RuntimeException(String.format(
                            "Mismatched parentheses: extra ')' at position %d", i));
                }
                ops.pop(); // 弹出左括号（不加入输出）
                i++;
            }

            // 4. 处理运算符（含一元正负号）
            else if (isOperator(ch)) {
                // 判断是否为一元运算符（正负号）：满足以下任一条件
                // - 位于表达式开头
                // - 前一个字符是左括号
                // - 前一个字符是其他运算符
                boolean isUnaryOp = (i == 0)
                        || (expr.charAt(i - 1) == '(')
                        || (isOperator(expr.charAt(i - 1)));

                if (isUnaryOp && (ch == '+' || ch == '-')) {
                    output.add("0"); // 用 "0+num" 表示 "+num"，"0-num" 表示 "-num"
                }

                // 处理运算符优先级和结合性
                while (!ops.isEmpty() && isOperator(ops.peek())) {
                    char topOp = ops.peek();
                    // 左结合：当前优先级 <= 栈顶优先级 → 弹出
                    // 右结合（仅^）：当前优先级 < 栈顶优先级 → 弹出
                    boolean needPop = (isLeftAssociative(ch) && precedence(ch) <= precedence(topOp))
                            || (!isLeftAssociative(ch) && precedence(ch) < precedence(topOp));

                    if (needPop) {
                        output.add(String.valueOf(ops.pop()));
                    } else {
                        break;
                    }
                }

                ops.push(ch);
                i++;
            }

            // 5. 非法字符
            else {
                throw new RuntimeException(String.format(
                        "Unexpected character '%c' at position %d", ch, i));
            }
        }

        // 6. 弹出栈中剩余运算符
        while (!ops.isEmpty()) {
            char remainingOp = ops.pop();
            if (remainingOp == '(' || remainingOp == ')') {
                throw new RuntimeException("Mismatched parentheses: extra '(' in expression");
            }
            output.add(String.valueOf(remainingOp));
        }

        return output;
    }

    // 逆波兰表达式（RPN）求值
    private static double evalRPN(List<String> tokens) {
        Deque<Double> stack = new ArrayDeque<>();

        for (String token : tokens) {
            if (isNumber(token)) {
                // 此处无需再次校验格式（toRPN已严格校验）
                stack.push(Double.parseDouble(token));
            } else if (isOperator(token.charAt(0))) {
                // 检查栈中操作数是否足够
                if (stack.size() < 2) {
                    throw new RuntimeException("Invalid expression: insufficient operands for operator '" + token + "'");
                }
                // 右操作数（后入栈）
                double right = stack.pop();
                // 左操作数（先入栈）
                double left = stack.pop();
                double result = 0.0;

                switch (token.charAt(0)) {
                    case '+' -> result = left + right;
                    case '-' -> result = left - right;
                    case '*' -> result = left * right;
                    case '/' -> {
                        if (right == 0) {
                            throw new RuntimeException("Calculation error: division by zero");
                        }
                        result = left / right;
                    }
                    case '^' -> {
                        // 处理特殊值：负数的非整数次幂（无实数解）
                        if (left < 0 && !isInteger(right)) {
                            throw new RuntimeException(String.format(
                                    "Calculation error: negative base (%.2f) with non-integer exponent (%.2f)",
                                    left, right));
                        }
                        // 处理 0^0（数学未定义，根据业务抛异常）
                        if (left == 0 && right == 0) {
                            throw new RuntimeException("Calculation error: 0^0 is undefined");
                        }
                        result = Math.pow(left, right);
                    }
                    default -> throw new RuntimeException("Unexpected operator: '" + token + "'");
                }

                stack.push(result);
            } else {
                throw new RuntimeException("Invalid token in RPN: '" + token + "'");
            }
        }

        // 检查栈中结果是否唯一（避免表达式不完整）
        if (stack.size() != 1) {
            throw new RuntimeException("Invalid expression: incomplete or extra operands");
        }

        return stack.pop();
    }

    // 判断是否为数字（复用正则，确保与toRPN校验逻辑一致）
    private static boolean isNumber(String s) {
        return NUMBER_PATTERN.matcher(s).matches();
    }

    // 判断是否为运算符
    private static boolean isOperator(char ch) {
        return "+-*/^".indexOf(ch) != -1;
    }

    // 获取运算符优先级（数字越大优先级越高）
    private static int precedence(char op) {
        return switch (op) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            case '^' -> 3;
            default -> -1;
        };
    }

    // 判断运算符结合性（仅^为右结合，其余为左结合）
    private static boolean isLeftAssociative(char op) {
        return op != '^';
    }

    // 判断是否为整数（用于乘方特殊值校验）
    private static boolean isInteger(double num) {
        // 避免浮点数精度问题（如 2.0000000001 视为非整数）
        return Math.abs(num - Math.floor(num)) < 1e-9 && !Double.isInfinite(num);
    }
//
//    // 测试用例（覆盖常规场景和边界场景）
//    public static void main(String[] args) {
//        // 常规场景
//        System.out.println("3+4*2/(1-5)^2 = " + eval("3+4*2/(1-5)^2")); // 3.5
//        System.out.println("-3+5 = " + eval("-3+5"));                 // 2.0
//        System.out.println("+5-3 = " + eval("+5-3"));                 // 2.0
//        System.out.println("2*(-4+6) = " + eval("2*(-4+6)"));         // 4.0
//        System.out.println("12.34+0.66 = " + eval("12.34+0.66"));     // 13.0
//        System.out.println("(3)+-2 = " + eval("(3)+-2"));             // 1.0
//        System.out.println("2^(3+1) = " + eval("2^(3+1)"));           // 16.0
//
//        // 异常场景（应输出NaN并打印错误日志）
//        System.out.println("12.34.56+1 = " + eval("12.34.56+1"));     // 多小数点
//        System.out.println("3/0 = " + eval("3/0"));                   // 除以零
//        System.out.println("(-2)^0.5 = " + eval("(-2)^0.5"));         // 负数非整数次幂
//        System.out.println("0^0 = " + eval("0^0"));                   // 0^0未定义
//        System.out.println("(3+4 = " + eval("(3+4"));                 // 括号不匹配
//        System.out.println("3+a = " + eval("3+a"));                   // 非法字符
//    }
}