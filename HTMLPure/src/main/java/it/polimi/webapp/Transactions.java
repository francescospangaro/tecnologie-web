package it.polimi.webapp;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public class Transactions {

    private Transactions() {
    }

    public static <E extends Throwable, T> T start(Connection connection,
                                                   Type type,
                                                   SqlFunction<E, T> fn) throws SQLException, E {
        return switch (type) {
            case NESTED -> startNestedTransaction(connection, fn);
            case NEW_REQUIRED -> startRequiredNewTransaction(connection, fn);
        };
    }

    @SuppressWarnings("NullAway") // Generics are not properly supported
    public static <E extends Throwable, T> @Nullable T startNullable(Connection connection,
                                                                     Type type,
                                                                     NullableSqlFunction<E, T> fn) throws SQLException, E {
        return switch (type) {
            case NESTED -> startNestedTransaction(connection, fn::apply);
            case NEW_REQUIRED -> startRequiredNewTransaction(connection, fn::apply);
        };
    }

    @SuppressWarnings("NullAway") // Generics are not properly supported
    private static <E extends Throwable, T> T startRequiredNewTransaction(Connection connection, SqlFunction<E, T> fn) throws SQLException, E {
        if (!connection.getAutoCommit())
            throw new SQLException("New transaction required, but was already in one");

        connection.setAutoCommit(false);
        try {
            var res = fn.apply(connection);
            connection.commit();
            return res;
        } catch (Throwable t) {
            connection.rollback();
            throw t;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @SuppressWarnings("NullAway") // Generics are not properly supported
    private static <E extends Throwable, T> T startNestedTransaction(Connection connection, SqlFunction<E, T> fn) throws SQLException, E {
        boolean openedTransaction = connection.getAutoCommit();
        if (openedTransaction)
            connection.setAutoCommit(false);
        var savePoint = connection.setSavepoint();

        try {
            var res = fn.apply(connection);
            if (openedTransaction)
                connection.commit();
            return res;
        } catch (Throwable t) {
            connection.rollback(savePoint);
            throw t;
        } finally {
            if (openedTransaction)
                connection.setAutoCommit(true);
        }
    }

    public interface SqlFunction<E extends Throwable, T> {

        T apply(Connection tx) throws SQLException, E;
    }

    public interface NullableSqlFunction<E extends Throwable, T> {

        @Nullable T apply(Connection tx) throws SQLException, E;
    }

    public enum Type {
        NESTED, NEW_REQUIRED
    }
}
