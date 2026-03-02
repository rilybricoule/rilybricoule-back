package com.sbsolutions.rilybricoule.security;

import com.sbsolutions.rilybricoule.mapper.CouponMapper;
import com.sbsolutions.rilybricoule.repository.ClientRepository;
import com.sbsolutions.rilybricoule.repository.CouponRepository;
import com.sbsolutions.rilybricoule.repository.PrestaireRepository;
import com.sbsolutions.rilybricoule.security.domain.port.in.AuthUseCase;
import com.sbsolutions.rilybricoule.services.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Role-Based Endpoint Access Tests")
class RoleBasedAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private AuthUseCase authUseCase;
    @MockBean private ClientRepository clientRepository;
    @MockBean private PrestaireRepository prestaireRepository;
    @MockBean private GeocodingService geocodingService;
    @MockBean private ReservationService reservationService;
    @MockBean private CouponRepository couponRepository;
    @MockBean private CouponService couponService;
    @MockBean private CouponMapper couponMapper;
    @MockBean private AvisService avisService;
    @MockBean private IChatService chatService;
    @MockBean private IMessageService messageService;
    @MockBean private INotificationService notificationService;
    @MockBean private PaymentService paymentService;

    @Nested
    @DisplayName("Public endpoints (no auth)")
    class PublicEndpoints {

        @Test
        @DisplayName("POST /api/auth/register -> accessible (no auth)")
        void authRegister_NoAuth_Accessible() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"firstName\":\"Ahmed\",\"lastName\":\"Ben\",\"email\":\"a@b.com\"," +
                                    "\"password\":\"12345678\",\"phone\":\"0612345678\",\"role\":\"CLIENT\"}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @DisplayName("POST /api/auth/login -> accessible (no auth)")
        void authLogin_NoAuth_Accessible() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"a@b.com\",\"password\":\"pass1234\"}"))
                    .andExpect(status().is(not(403)));
        }
    }

    @Nested
    @DisplayName("Unauthenticated requests -> 401")
    class UnauthenticatedAccess {

        @Test
        @DisplayName("GET /api/clients -> 401")
        void clients_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/clients"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/prestataires -> 401")
        void prestataires_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/prestataires"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/reservations/1 -> 401")
        void reservations_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/reservations/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/coupons -> 401")
        void coupons_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/coupons"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/avis/prestataire/1 -> 401")
        void avis_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/avis/prestataire/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/chats/start -> 401")
        void chats_NoAuth_Returns401() throws Exception {
            mockMvc.perform(post("/api/chats/start")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/notifications/user/1 -> 401")
        void notifications_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/notifications/user/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("CLIENT role access")
    class ClientRoleAccess {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("GET /api/clients -> 200 (CLIENT allowed)")
        void getClients_AsClient_Allowed() throws Exception {
            mockMvc.perform(get("/api/clients"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("PUT /api/clients/1 -> allowed (CLIENT can update via @PreAuthorize)")
        void updateClient_AsClient_Allowed() throws Exception {
            mockMvc.perform(put("/api/clients/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("DELETE /api/clients/1 -> 403 (CLIENT cannot delete)")
        void deleteClient_AsClient_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/clients/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("POST /api/clients -> 403 (CLIENT cannot create)")
        void createClient_AsClient_Forbidden() throws Exception {
            mockMvc.perform(post("/api/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("GET /api/prestataires -> 200 (CLIENT allowed via @PreAuthorize)")
        void getPrestataires_AsClient_Allowed() throws Exception {
            mockMvc.perform(get("/api/prestataires"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("POST /api/prestataires -> 403 (CLIENT cannot create prestataire)")
        void createPrestataire_AsClient_Forbidden() throws Exception {
            mockMvc.perform(post("/api/prestataires")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("POST /api/reservations -> allowed (CLIENT can create reservations)")
        void createReservation_AsClient_Allowed() throws Exception {
            mockMvc.perform(post("/api/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("PATCH /api/reservations/1/status -> 403 (CLIENT cannot change status)")
        void updateReservationStatus_AsClient_Forbidden() throws Exception {
            mockMvc.perform(patch("/api/reservations/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"ACCEPTED\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("GET /api/coupons -> 200 (CLIENT can read coupons)")
        void getCoupons_AsClient_Allowed() throws Exception {
            mockMvc.perform(get("/api/coupons"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("POST /api/coupons -> 403 (CLIENT cannot create coupons)")
        void createCoupon_AsClient_Forbidden() throws Exception {
            mockMvc.perform(post("/api/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("POST /api/avis -> allowed (CLIENT can post reviews)")
        void createAvis_AsClient_Allowed() throws Exception {
            mockMvc.perform(post("/api/avis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("POST /api/chats/start -> allowed (CLIENT can chat)")
        void startChat_AsClient_Allowed() throws Exception {
            mockMvc.perform(post("/api/chats/start")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("GET /api/notifications/user/1 -> allowed (CLIENT can read notifications)")
        void getNotifications_AsClient_Allowed() throws Exception {
            mockMvc.perform(get("/api/notifications/user/1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PRESTATAIRE role access")
    class PrestataireRoleAccess {

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("GET /api/prestataires -> 200 (PRESTATAIRE allowed)")
        void getPrestataires_AsPrestataire_Allowed() throws Exception {
            mockMvc.perform(get("/api/prestataires"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("PUT /api/prestataires/1 -> allowed (PRESTATAIRE can update via @PreAuthorize)")
        void updatePrestataire_AsPrestataire_Allowed() throws Exception {
            mockMvc.perform(put("/api/prestataires/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("DELETE /api/prestataires/1 -> 403 (PRESTATAIRE cannot delete)")
        void deletePrestataire_AsPrestataire_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/prestataires/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("GET /api/clients -> 403 (PRESTATAIRE cannot read clients)")
        void getClients_AsPrestataire_Forbidden() throws Exception {
            mockMvc.perform(get("/api/clients"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("POST /api/reservations -> 403 (PRESTATAIRE cannot create reservations)")
        void createReservation_AsPrestataire_Forbidden() throws Exception {
            mockMvc.perform(post("/api/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("PATCH /api/reservations/1/status -> allowed (PRESTATAIRE can accept/refuse)")
        void updateReservationStatus_AsPrestataire_Allowed() throws Exception {
            mockMvc.perform(patch("/api/reservations/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"ACCEPTED\"}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("GET /api/reservations/prestataire/1 -> 200 (PRESTATAIRE can read reservations)")
        void getReservations_AsPrestataire_Allowed() throws Exception {
            mockMvc.perform(get("/api/reservations/prestataire/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("POST /api/coupons -> 403 (PRESTATAIRE cannot manage coupons)")
        void createCoupon_AsPrestataire_Forbidden() throws Exception {
            mockMvc.perform(post("/api/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("POST /api/avis -> 403 (PRESTATAIRE cannot post reviews)")
        void createAvis_AsPrestataire_Forbidden() throws Exception {
            mockMvc.perform(post("/api/avis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("POST /api/chats/start -> allowed (PRESTATAIRE can chat)")
        void startChat_AsPrestataire_Allowed() throws Exception {
            mockMvc.perform(post("/api/chats/start")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(not(403)));
        }
    }

    @Nested
    @DisplayName("ADMIN role access")
    class AdminRoleAccess {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /api/clients -> 200 (ADMIN allowed)")
        void getClients_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(get("/api/clients"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/clients -> allowed (ADMIN can create clients)")
        void createClient_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(post("/api/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("DELETE /api/clients/1 -> allowed (ADMIN can delete clients)")
        void deleteClient_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(delete("/api/clients/1"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /api/prestataires -> 200 (ADMIN allowed)")
        void getPrestataires_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(get("/api/prestataires"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/prestataires -> allowed (ADMIN can create prestataires)")
        void createPrestataire_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(post("/api/prestataires")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("DELETE /api/prestataires/1 -> allowed (ADMIN can delete)")
        void deletePrestataire_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(delete("/api/prestataires/1"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/coupons -> allowed (ADMIN can create coupons)")
        void createCoupon_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(post("/api/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("PUT /api/coupons/1 -> allowed (ADMIN can update coupons)")
        void updateCoupon_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(put("/api/coupons/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("DELETE /api/coupons/1 -> allowed (ADMIN can delete coupons)")
        void deleteCoupon_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(delete("/api/coupons/1"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("PATCH /api/reservations/1/status -> allowed (ADMIN can change status)")
        void updateReservationStatus_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(patch("/api/reservations/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"ACCEPTED\"}"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/reservations/1/cancel -> allowed (ADMIN can cancel)")
        void cancelReservation_AsAdmin_Allowed() throws Exception {
            mockMvc.perform(post("/api/reservations/1/cancel"))
                    .andExpect(status().is(not(403)));
        }
    }

    @Nested
    @DisplayName("Cross-role forbidden checks")
    class CrossRoleForbidden {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("CLIENT -> DELETE /api/prestataires/1 -> 403")
        void deletePrestataire_AsClient_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/prestataires/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("CLIENT -> PUT /api/prestataires/1 -> 403")
        void updatePrestataire_AsClient_Forbidden() throws Exception {
            mockMvc.perform(put("/api/prestataires/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("PRESTATAIRE -> POST /api/clients -> 403")
        void createClient_AsPrestataire_Forbidden() throws Exception {
            mockMvc.perform(post("/api/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("PRESTATAIRE -> DELETE /api/clients/1 -> 403")
        void deleteClient_AsPrestataire_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/clients/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "PRESTATAIRE")
        @DisplayName("PRESTATAIRE -> DELETE /api/coupons/1 -> 403")
        void deleteCoupon_AsPrestataire_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/coupons/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("CLIENT -> PUT /api/coupons/1 -> 403")
        void updateCoupon_AsClient_Forbidden() throws Exception {
            mockMvc.perform(put("/api/coupons/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("CLIENT -> DELETE /api/coupons/1 -> 403")
        void deleteCoupon_AsClient_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/coupons/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
