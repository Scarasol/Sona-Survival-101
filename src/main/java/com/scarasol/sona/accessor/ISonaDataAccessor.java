package com.scarasol.sona.accessor;

import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Scarasol
 */
public interface ISonaDataAccessor {

    Map<String, Supplier<?>> getSonaDataMap();

    default void copySonaData(ISonaDataAccessor oldDataAccessor) {
        Map<String, Supplier<?>> oldMap = oldDataAccessor.getSonaDataMap();
        Map<String, Supplier<?>> newMap = getSonaDataMap();
        newMap.clear();
        newMap.putAll(oldMap);
    }

    default void putSonaData(String string, Supplier<?> supplier) {
        getSonaDataMap().put(string, supplier);
    }

    default Supplier<?> removeSonaData(String string) {
        return getSonaDataMap().remove(string);
    }

    default Supplier<?> getSonaData(String string) {
        return getSonaDataMap().getOrDefault(string, () -> null);
    }

    default boolean getBooleanSonaData(String string) {
        Supplier<?> supplier = getSonaData(string);
        if (supplier.get() instanceof Boolean booleanData) {
            return booleanData;
        }
        return false;
    }

    default double getDoubleSonaData(String string) {
        Supplier<?> supplier = getSonaData(string);
        if (supplier.get() instanceof Double doubleData) {
            return doubleData;
        }
        return 0;
    }

    default float getFloatSonaData(String string) {
        Supplier<?> supplier = getSonaData(string);
        if (supplier.get() instanceof Float floatData) {
            return floatData;
        }
        return 0;
    }

    default int getIntSonaData(String string) {
        Supplier<?> supplier = getSonaData(string);
        if (supplier.get() instanceof Integer intData) {
            return intData;
        }
        return 0;
    }

    default long getLongSonaData(String string) {
        Supplier<?> supplier = getSonaData(string);
        if (supplier.get() instanceof Long longData) {
            return longData;
        }
        return 0;
    }

    default String getStringSonaData(String string) {
        Supplier<?> supplier = getSonaData(string);
        if (supplier.get() instanceof String strData) {
            return strData;
        }
        return "";
    }
}
