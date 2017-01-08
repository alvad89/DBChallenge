package info;

import info.entity.Room;
import info.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 *
 */
@Warmup(iterations = 0)
@Measurement(iterations = 10000)
@BenchmarkMode({Mode.SingleShotTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Threads(1)
@Fork(1)
public class App 
{
    static JdbcTemplate jdbcTemplate;
    static AtomicInteger atomicInteger = new AtomicInteger();
    static SessionFactory sessionPostgres;
    private static AtomicInteger atomicInteger2 = new AtomicInteger();


    @Setup
    public static void setUp() {
        System.out.println("set up");
        System.setProperty("spring.profiles.active", "h2");
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-beans.xml");
        jdbcTemplate = context.getBean(JdbcTemplate.class);
        LocalSessionFactoryBean localSessionFactoryBean  = context.getBean(LocalSessionFactoryBean.class);
        sessionPostgres = localSessionFactoryBean.getObject();
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(App.class.getSimpleName())
                .threads(1)
                .forks(1)
                .build();
        new Runner(options).run();

        //jdbctemplate
        //3373 mc для batch для h2 для 100000 записей
        //4070 mc для batch postgre для 100000 записей
//        setUp();
//        long l = System.currentTimeMillis();
//        batch();
//        System.out.println(System.currentTimeMillis() - l);
    }

//    @Benchmark
    public void generate() {
            jdbcTemplate.update("INSERT INTO room" +
                    " VALUES (" + atomicInteger.get() + "," +
                    "'rooms" + atomicInteger.get() + "')");
            jdbcTemplate.update("INSERT INTO \"user\"" +
                    " VALUES (" + atomicInteger.get() + "," +
                    "'usersnumber" +atomicInteger.get() + "',"+atomicInteger.getAndIncrement()+")");
    }

    @Benchmark
    public void generateHyber() {
        Room room = new Room();
        room.setId(atomicInteger.get());
        room.setRoomName("rooms "+ atomicInteger.get());

        User user = new User();
        user.setId(atomicInteger.get());
        user.setName("usersnumber "+ atomicInteger.get());
        user.setRoom_number(atomicInteger.getAndIncrement());

        Session session = sessionPostgres.openSession();
        Transaction tx = session.beginTransaction();
        session.save(room);
        session.save(user);
        tx.commit();
        if (session.isOpen()) {
            session.close();
        }

    }

    public static void batch() {
        final int batchSize = 10000;

        jdbcTemplate.batchUpdate("insert into room values( ? ,  ?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, atomicInteger.get());
                        ps.setString(2, "room " + atomicInteger.getAndIncrement());
                    }


                    public int getBatchSize() {
                        return batchSize;
                    }
                });

        jdbcTemplate.batchUpdate("insert into \"user\" values( ? , ? , ?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, atomicInteger2.get());
                        ps.setString(2, "name " + atomicInteger2.get());
                        ps.setInt(3, atomicInteger2.getAndIncrement());
                    }

                    public int getBatchSize() {
                        return batchSize;
                    }
                });
    }
}
