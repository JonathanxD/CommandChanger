package github.therealbuggy.commandchanger.config.transformer;

import java.util.StringJoiner;

/**
 * Created by jonathan on 19/03/16.
 */
public class Locals {

    protected static String get(String... strings) {
        StringJoiner stringJoiner = new StringJoiner(".");

        for (String s : strings) {
            stringJoiner.add(s);
        }

        return stringJoiner.toString();
    }

}
