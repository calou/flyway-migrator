import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.sql.ResultSet;

import static org.junit.Assert.*;

public class MigratorTest {

    @Test
    public void runNominal() throws Exception {
        Migrator.run(new String[]{"src/test/resources/h2.properties", "clean", "migrate"});

        DataSource ds = new DriverDataSource(Migrator.class.getClassLoader(), "org.h2.Driver", "jdbc:h2:./target/db", "sa", "");
        ResultSet rs = ds.getConnection().prepareStatement("SELECT * FROM people").executeQuery();
        rs.next();
        assertEquals(1, rs.getInt("id"));
        assertEquals("Paul", rs.getString("firstname"));
        assertEquals("Newman", rs.getString("lastname"));

        rs.next();
        assertEquals(2, rs.getInt("id"));
        assertEquals("Audrey", rs.getString("firstname"));
        assertEquals("Hepburn", rs.getString("lastname"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void runWithoutArguments() throws Exception {
        Migrator.run(new String[]{});
    }

    @Test(expected=FileNotFoundException.class)
    public void runWithInvalidDirectories() throws Exception {
        Migrator.run(new String[]{"src/test/resources/invalid_directories.properties", "clean"});
    }

    @Test(expected=IllegalArgumentException.class)
    public void runWithUnknownCommand() throws Exception {
        Migrator.run(new String[]{"src/test/resources/h2.properties", "clean", "unknown"});
    }
}