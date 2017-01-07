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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import java.applet.AppletContext;
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
    JdbcTemplate jdbcTemplate;
    AtomicInteger atomicInteger = new AtomicInteger();
    SessionFactory sessionPostgres;


    @Setup
    public void setUp() {
        System.out.println("set up");
        System.setProperty("spring.profiles.active", "postgres");
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
    }

//    @Benchmark
    public void generate() {
            jdbcTemplate.update("INSERT INTO ROOM" +
                    " VALUES (" + atomicInteger.get() + "," +
                    "'rooms" + atomicInteger.get() + "')");
            jdbcTemplate.update("INSERT INTO \"USER\"" +
                    " VALUES (" + atomicInteger.get() + "," +
                    "'usersnumber" +atomicInteger.get() + "',"+atomicInteger.getAndDecrement()+")");
    }

    @Benchmark
    public void generateHyber() {
        Room room = new Room();
        room.setId(atomicInteger.get());
        room.setRoomName("rooms "+ atomicInteger.get());

        User user = new User();
        user.setId(atomicInteger.get());
        user.setName("usersnumber "+ atomicInteger.get());
        user.setRoom_number(atomicInteger.getAndDecrement());

        Session session = sessionPostgres.openSession();
        Transaction tx = session.beginTransaction();
        session.save(room);
        session.save(user);
        tx.commit();
        if (session.isOpen()) {
            session.close();
        }

    }
}
