package yangyd.kf.ingester

import java.util.concurrent.{Executors, ScheduledExecutorService}

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {
  @Bean
  def executor: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
}
