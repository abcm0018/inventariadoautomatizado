package com.abcm0018.inventarioautomatizado.auth.service;

import com.abcm0018.inventarioautomatizado.auth.domain.entity.EmailTemplate;
import com.abcm0018.inventarioautomatizado.auth.domain.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final SpringTemplateEngine templateEngine;

    public String buildBody(String templateName, String name, String surname, String employeeNumber, String password) {
        EmailTemplate emailTemplate = emailTemplateRepository.findByTemplateName(templateName).orElseThrow(() -> new RuntimeException("No existe la plantilla: " + templateName));

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("surname", surname);
        context.setVariable("employeeNumber", employeeNumber);
        context.setVariable("password", password);

        return templateEngine.process(emailTemplate.getBody(), context);
    }

    public String getSubject(String templateName){
        return emailTemplateRepository.findByTemplateName(templateName).map(EmailTemplate::getSubject).orElseThrow(() -> new RuntimeException("No existe la plantilla: " + templateName));
    }
}
