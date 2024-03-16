package com.example.cryptoservice.controllrers;

import com.example.cryptoservice.controllers.IdCardController;
import com.example.cryptoservice.dto.IdCardDTO;
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
        IdCardDTO idCardDTO = new IdCardDTO(idCardNo);

        ResultActions resultActions = mockMvc.perform(post("/id-card/encrypt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idCardDTO)))
                .andExpect(status().isOk());

        String encrytedIdCard = resultActions.andReturn().getResponse().getContentAsString();

        IdCardDTO idCardDTO1 = objectMapper.readValue(encrytedIdCard, IdCardDTO.class);

        resultActions = mockMvc.perform(post("/id-card/decrypt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idCardDTO1)))
                .andExpect(status().isOk());

        String idCardNoDecrypted = resultActions.andReturn().getResponse().getContentAsString();

        IdCardDTO idCardDecrypted = objectMapper.readValue(idCardNoDecrypted, IdCardDTO.class);

        assert idCardDTO.equals(idCardDecrypted);
    }
}
