/*
package org.sid.commandeservice.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void envoyerMessage(String message) {
        kafkaTemplate.send("commande-topic", message);
    }
}

*/
