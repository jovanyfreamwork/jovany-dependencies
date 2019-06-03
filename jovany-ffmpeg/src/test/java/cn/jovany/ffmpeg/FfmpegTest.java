package cn.jovany.ffmpeg;

import java.io.File;
import java.math.BigDecimal;

import org.apache.oro.text.regex.MalformedPatternException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;;

@SpringBootApplication
@SpringBootTest
@RunWith(SpringRunner.class)
public class FfmpegTest {

	public static void main(String[] args) {
		SpringApplication.run(FfmpegTest.class, args);
	}

	@Bean
	public Ffmpeg ffmpeg() {
		return new Ffmpeg(new File("/usr/local/Cellar/ffmpeg/4.1.1/bin/ffmpeg")).error(Throwable::printStackTrace);
	}

	@Test
	@Autowired
	public void testFfmpeg(Ffmpeg ffmpeg) throws MalformedPatternException {
		Integer durationByMillisecond = ffmpeg.build()
				.append("-i",
						new File("/Users/wangqi/ffmpegDir/47402cfd-82b4-11e9-86c3-005056b13ddf.mp4")::getAbsoluteFile)
				.execute(t -> t.regex("Duration: (.*?), start: (.*?), bitrate: (.*?)", 1).indexOf(0)
						.get(t1 -> t1.regex("(\\d\\d?):(\\d\\d?):(\\d\\d?)\\.(\\d\\d?)", 4).apply(re1 -> {
							BigDecimal hour = new BigDecimal(re1.group(0));
							BigDecimal minute = new BigDecimal(re1.group(1));
							BigDecimal second = new BigDecimal(re1.group(2));
							BigDecimal millisecond = new BigDecimal(re1.group(3));
							BigDecimal v60 = new BigDecimal(60);
							BigDecimal v100 = new BigDecimal(100);
							BigDecimal v1000 = new BigDecimal(1000);
							BigDecimal duration = millisecond.multiply(v100)
									.add(second.add(minute.multiply(v60)).multiply(v1000))
									.add(hour.multiply(v60).multiply(v60).multiply(v1000));
							return duration.intValue();
						})));
		System.out.println(durationByMillisecond);
	}

}
