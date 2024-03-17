package com.example.cryptoservice.controllrers;

import com.example.cryptoservice.controllers.IdCardController;
import com.example.cryptoservice.dto.IdCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IdCardController.class)
@AutoConfigureMockMvc
class IdCardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @InjectMocks
    private IdCardController idCardController;

    @Test
    void TestEncryptAndDecrypt() throws Exception {
        String idCardNo = "1100214356753";
        IdCard idCard = new IdCard(idCardNo);

        ResultActions resultActions = mockMvc.perform(post("/id-card/encrypt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idCard)))
                .andExpect(status().isOk());

        String encrytedIdCard = resultActions.andReturn().getResponse().getContentAsString();

        IdCard idCard1 = objectMapper.readValue(encrytedIdCard, IdCard.class);

        resultActions = mockMvc.perform(post("/id-card/decrypt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idCard1)))
                .andExpect(status().isOk());

        String idCardNoDecrypted = resultActions.andReturn().getResponse().getContentAsString();

        IdCard idCardDecrypted = objectMapper.readValue(idCardNoDecrypted, IdCard.class);

        assert idCard.equals(idCardDecrypted);
    }
}
