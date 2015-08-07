import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Migrator {
    private static String databaseUrl;
    private static String databaseUsername;
    private static String databasePassword;
    private static String databaseDriverClass;
    private static String[] migrationsDirectories;
    private static String migrationPrefix;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }
        readProperties(args[0]);
        DataSource dataSource = getDataSource();
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSqlMigrationPrefix(migrationPrefix);
        flyway.setLocations(migrationsDirectories);
        for (int i = 1; i < args.length; i++) {
            final String command = args[i];
            if ("clean".equals(command)) {
                flyway.clean();
            } else if ("migrate".equals(command)) {
                flyway.migrate();
            } else if ("repair".equals(command)) {
                flyway.repair();
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("    java -jar flyway-migrator.jar <file.properties> <commands>");
        System.out.println("Commands : ");
        System.out.println("    clean");
        System.out.println("    migrate");
        System.out.println("    repair");
    }

    private static DataSource getDataSource() {
        return new DriverDataSource(Migrator.class.getClassLoader(), databaseDriverClass, databaseUrl, databaseUsername, databasePassword);
    }

    private static void readProperties(String filePath) throws IOException {
        Properties properties = new Properties();
        final Path path = Paths.get(filePath).toAbsolutePath();
        try {
            properties.load(Files.newInputStream(path));
        } catch (IOException e) {
            System.out.println("Error:\nProperties file not found : " + path.toString());
            System.exit(1);
        }
        databaseUrl = properties.getProperty("url");
        databaseUsername = properties.getProperty("username");
        databasePassword = properties.getProperty("password");
        databaseDriverClass = properties.getProperty("driverClass");
        migrationPrefix = properties.getProperty("migrationPrefix", "");
        final String[] directories = properties.getProperty("migrationsDirectories").split(",");

        migrationsDirectories = new String[directories.length];
        for (int i = 0; i < directories.length; i++) {
            final Path directoryPath = Paths.get(directories[i]).toAbsolutePath();
            if (!Files.exists(directoryPath)) {
                System.out.println("Error:\nMigrations directory not found : " + directoryPath.toString());
                System.exit(1);
            }
            System.out.println("Using migrations directory : " + directoryPath.toString());
            migrationsDirectories[i] = "filesystem:" + directories[i];
        }

    }

}
