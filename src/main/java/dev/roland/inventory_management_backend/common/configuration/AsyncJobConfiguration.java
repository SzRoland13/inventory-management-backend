package dev.roland.inventory_management_backend.common.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.roland.inventory_management_backend.common.async.AsyncJobType;

/** RabbitMQ topology and durable asynchronous job publisher configuration. */
@Configuration
@EnableScheduling
public class AsyncJobConfiguration {

  public static final String EXCHANGE = "inventory.jobs";

  /**
   * Declares the durable exchange used by asynchronous jobs.
   *
   * @return durable jobs exchange
   */
  @Bean
  DirectExchange jobsExchange() {
    return new DirectExchange(EXCHANGE, true, false);
  }

  /**
   * Declares the primary email delivery queue.
   *
   * @return durable email queue
   */
  @Bean
  Queue emailQueue() {
    return QueueBuilder.durable(AsyncJobType.EMAIL.getQueueName())
        .deadLetterExchange(EXCHANGE)
        .deadLetterRoutingKey(AsyncJobType.EMAIL.getDeadLetterRoutingKey())
        .build();
  }

  /**
   * Declares the primary document PDF queue.
   *
   * @return durable document PDF queue
   */
  @Bean
  Queue documentPdfQueue() {
    return QueueBuilder.durable(AsyncJobType.DOCUMENT_PDF.getQueueName())
        .deadLetterExchange(EXCHANGE)
        .deadLetterRoutingKey(AsyncJobType.DOCUMENT_PDF.getDeadLetterRoutingKey())
        .build();
  }

  /**
   * Declares a delayed retry queue for email jobs.
   *
   * @param delay retry delay in milliseconds
   * @return durable email retry queue
   */
  @Bean
  Queue emailRetryQueue(@Value("${app.outbox.retry-delay-ms:3000}") final int delay) {
    return retryQueue(
        AsyncJobType.EMAIL.getRetryQueueName(), AsyncJobType.EMAIL.getRoutingKey(), delay);
  }

  /**
   * Declares a delayed retry queue for document PDF jobs.
   *
   * @param delay retry delay in milliseconds
   * @return durable document PDF retry queue
   */
  @Bean
  Queue documentPdfRetryQueue(@Value("${app.outbox.retry-delay-ms:3000}") final int delay) {
    return retryQueue(
        AsyncJobType.DOCUMENT_PDF.getRetryQueueName(),
        AsyncJobType.DOCUMENT_PDF.getRoutingKey(),
        delay);
  }

  /**
   * Builds a queue that routes messages back to their primary queue after a delay.
   *
   * @param name retry queue name
   * @param route primary routing key
   * @param delay retry delay in milliseconds
   * @return configured retry queue
   */
  private Queue retryQueue(final String name, final String route, final int delay) {
    return QueueBuilder.durable(name)
        .ttl(delay)
        .deadLetterExchange(EXCHANGE)
        .deadLetterRoutingKey(route)
        .build();
  }

  /**
   * Declares the email dead-letter queue.
   *
   * @return durable email dead-letter queue
   */
  @Bean
  Queue emailDeadLetterQueue() {
    return QueueBuilder.durable(AsyncJobType.EMAIL.getDeadLetterQueueName()).build();
  }

  /**
   * Declares the document PDF dead-letter queue.
   *
   * @return durable document PDF dead-letter queue
   */
  @Bean
  Queue documentPdfDeadLetterQueue() {
    return QueueBuilder.durable(AsyncJobType.DOCUMENT_PDF.getDeadLetterQueueName()).build();
  }

  /**
   * Binds the email queue to its routing key.
   *
   * @param emailQueue primary email queue
   * @param jobsExchange asynchronous jobs exchange
   * @return email queue binding
   */
  @Bean
  Binding emailBinding(
      @Qualifier("emailQueue") final Queue emailQueue, final DirectExchange jobsExchange) {
    return BindingBuilder.bind(emailQueue)
        .to(jobsExchange)
        .with(AsyncJobType.EMAIL.getRoutingKey());
  }

  /**
   * Binds the document PDF queue to its routing key.
   *
   * @param documentPdfQueue primary document PDF queue
   * @param jobsExchange asynchronous jobs exchange
   * @return document PDF queue binding
   */
  @Bean
  Binding documentPdfBinding(
      @Qualifier("documentPdfQueue") final Queue documentPdfQueue,
      final DirectExchange jobsExchange) {
    return BindingBuilder.bind(documentPdfQueue)
        .to(jobsExchange)
        .with(AsyncJobType.DOCUMENT_PDF.getRoutingKey());
  }

  /**
   * Binds the email retry queue to its routing key.
   *
   * @param emailRetryQueue email retry queue
   * @param jobsExchange asynchronous jobs exchange
   * @return email retry queue binding
   */
  @Bean
  Binding emailRetryBinding(
      @Qualifier("emailRetryQueue") final Queue emailRetryQueue,
      final DirectExchange jobsExchange) {
    return BindingBuilder.bind(emailRetryQueue)
        .to(jobsExchange)
        .with(AsyncJobType.EMAIL.getRetryRoutingKey());
  }

  /**
   * Binds the document PDF retry queue to its routing key.
   *
   * @param documentPdfRetryQueue document PDF retry queue
   * @param jobsExchange asynchronous jobs exchange
   * @return document PDF retry queue binding
   */
  @Bean
  Binding pdfRetryBinding(
      @Qualifier("documentPdfRetryQueue") final Queue documentPdfRetryQueue,
      final DirectExchange jobsExchange) {
    return BindingBuilder.bind(documentPdfRetryQueue)
        .to(jobsExchange)
        .with(AsyncJobType.DOCUMENT_PDF.getRetryRoutingKey());
  }

  /**
   * Binds the email dead-letter queue to its routing key.
   *
   * @param emailDeadLetterQueue email dead-letter queue
   * @param jobsExchange asynchronous jobs exchange
   * @return email dead-letter queue binding
   */
  @Bean
  Binding emailDlqBinding(
      @Qualifier("emailDeadLetterQueue") final Queue emailDeadLetterQueue,
      final DirectExchange jobsExchange) {
    return BindingBuilder.bind(emailDeadLetterQueue)
        .to(jobsExchange)
        .with(AsyncJobType.EMAIL.getDeadLetterRoutingKey());
  }

  /**
   * Binds the document PDF dead-letter queue to its routing key.
   *
   * @param documentPdfDeadLetterQueue document PDF dead-letter queue
   * @param jobsExchange asynchronous jobs exchange
   * @return document PDF dead-letter queue binding
   */
  @Bean
  Binding pdfDlqBinding(
      @Qualifier("documentPdfDeadLetterQueue") final Queue documentPdfDeadLetterQueue,
      final DirectExchange jobsExchange) {
    return BindingBuilder.bind(documentPdfDeadLetterQueue)
        .to(jobsExchange)
        .with(AsyncJobType.DOCUMENT_PDF.getDeadLetterRoutingKey());
  }

  /**
   * Creates the JSON message converter used by RabbitMQ.
   *
   * @param objectMapper configured Jackson mapper
   * @return Jackson message converter
   */
  @Bean
  Jackson2JsonMessageConverter jackson2JsonMessageConverter(final ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  /**
   * Configures the RabbitTemplate for mandatory, JSON-based publication.
   *
   * @param connectionFactory RabbitMQ connection factory
   * @param messageConverter JSON message converter
   * @return configured RabbitTemplate
   */
  @Bean
  RabbitTemplate rabbitTemplate(
      final ConnectionFactory connectionFactory,
      final Jackson2JsonMessageConverter messageConverter) {
    final RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter);
    template.setMandatory(true);
    return template;
  }
}
