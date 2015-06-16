package com.profi.spring.boot.starter.camunda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.transaction.Transactional;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.profi.spring.boot.starter.camunda.test.TestApplication;
import com.profi.spring.boot.starter.camunda.test.jpa.domain.TestEntity;
import com.profi.spring.boot.starter.camunda.test.jpa.repository.TestEntityRepository;
import com.profi.spring.boot.starter.camunda.test.service.TransactionalTestService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestApplication.class })
@Transactional
public class CamundaJpaAutoConfigurationIT extends AbstractCamundaAutoConfigurationIT {

    @Autowired
    private TestEntityRepository testEntityRepository;

    @Autowired
    private TransactionalTestService transactionalTestService;

    @Test
    public void jpaTest() {
        ProcessInstance processInstance = transactionalTestService.doOk();
        TestEntity testEntity = (TestEntity) runtimeService.getVariable(
                processInstance.getId(), "test");
        assertNotNull(testEntity);
        assertEquals("text", testEntity.getText());
        assertEquals(processInstance.getId(), runtimeService.createProcessInstanceQuery()
                .variableValueEquals("test", testEntity).singleResult().getId());
        testEntity.setText("text2");
        testEntityRepository.save(testEntity);
        testEntity = (TestEntity) runtimeService.getVariable(processInstance.getId(),
                "test");
        assertEquals("text2", testEntity.getText());
    }

    @Test
    public void transactionTest() {
        assertEquals(0, testEntityRepository.count());
        try {
            transactionalTestService.doThrowing();
            fail();
        } catch (IllegalStateException e) {
            assertEquals(0, testEntityRepository.count());
            assertEquals(0, runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey("TestProcess").count());
        }
    }
}