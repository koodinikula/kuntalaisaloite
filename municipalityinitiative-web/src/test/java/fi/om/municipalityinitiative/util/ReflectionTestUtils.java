package fi.om.municipalityinitiative.util;

import fi.om.municipalityinitiative.dto.NormalAuthor;
import fi.om.municipalityinitiative.dto.VerifiedAuthor;
import fi.om.municipalityinitiative.dto.service.Location;
import fi.om.municipalityinitiative.dto.service.Municipality;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.json.ObjectSerializer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class ReflectionTestUtils {

    /**
     * Sets random values to all fields recursively.
     */
    public static <T> T modifyAllFields(T bean)  {

        Class clazz = bean.getClass();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                    try {
                        field.set(bean, randomValue(field.getType()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
            }
            clazz = clazz.getSuperclass();
        }
        while (clazz != null && !clazz.equals(Object.class));

        try {
            assertNoNullFields(bean);
        } catch (AssertionError e) {
            fail("Unable to randomize all field values, something was null: " + e.getMessage());
        }
        return bean;
    }

    public static <T> void assertReflectionEquals(T o1, T o2) {
        assertThat("Reflected fields should match", ReflectionToStringBuilder.reflectionToString(o1, ToStringStyle.SHORT_PREFIX_STYLE),
                is(ReflectionToStringBuilder.reflectionToString(o2, ToStringStyle.SHORT_PREFIX_STYLE)));
    }

    private static Object randomValue(Class<?> type) throws IllegalAccessException {

        // standard Java types
        if (type.equals(String.class)) {
            return randomString();
        }
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return randomInt();
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return randomLong();
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return randomDouble();
        }
        if (type.equals(boolean.class)) {
            return true;
        }
        if (type.equals(Boolean.class)) {
            return true;
        }
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            int i = randomInt() % constants.length;
            if (i < 0) i*= -1;
            return constants[i];
        }
        if (type.equals(LocalDateTime.class)) {
            return new LocalDateTime(randomLong());
        }
        if (type.equals(LocalDate.class)){
            return new LocalDate(randomLong());
        }

        if (type.equals(DateTime.class)) {
            return new DateTime(randomInt());
        }
        if (type.equals(Optional.class)) {
            return Optional.empty(); // TODO: find out the type of the optional object and recursively generate a random non-absent value
        }

        if (type.equals(ContactInfo.class)) {
            return modifyAllFields(new ContactInfo());
        }

        if (type.equals(Municipality.class)) {
            return modifyAllFields(new Municipality(0, "", "", false));
        }
        if (type.equals(NormalAuthor.class)) {
            return modifyAllFields(new NormalAuthor());
        }
        if (type.equals(VerifiedAuthor.class)) {
            return modifyAllFields(new VerifiedAuthor());
        }
        if (type.equals(List.class)) {
            return new ArrayList<>();
        }
        if (type.equals(ArrayList.class)) {
            return new ArrayList<>();
        }
        if (type.equals(Location.class)) {
            return modifyAllFields(new Location());
        }
        throw new IllegalArgumentException("unsupported type: " + type);
    }

    private static double randomDouble() {
        return new Random().nextDouble();
    }

    /**
     * Asserts if any fields are null.
     * Uses json-serializer because it's simple, unfortunately failure messages are ugly.
     * @param o
     */
    public static void assertNoNullFields(Object o){
            assertThat(ObjectSerializer.objectToString(o), not(containsString(":null")));
    }

    private static int randomInt() {
        return new Random().nextInt();
    }

    private static long randomLong() {
        return new Random().nextLong();
    }

    private static String randomString() {
        return RandomStringUtils.randomAlphabetic(5);
    }

}
