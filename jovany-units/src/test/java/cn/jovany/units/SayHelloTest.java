package cn.jovany.units;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
public class SayHelloTest {

	public static void main(String[] args) {
		SpringApplication.run(SayHelloTest.class, args);
	}

	@Test
	public void contextLoads() {
		System.out.println("Hello, world!");
	}
}
