package pt.isec.skysystem.model.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isec.skysystem.model.data.dao.UserDAO;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseAuthServiceTest {

    private DatabaseAuthService authService;

    @Mock
    private UserDAO mockUserDAO; // Mockito cria o DAO falso automaticamente

    @BeforeEach
    void setUp() throws Exception {
        authService = new DatabaseAuthService();

        // Injeção do Mock via Reflection (igual ao AIRecommenderTest)
        Field daoField = DatabaseAuthService.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(authService, mockUserDAO);
    }

    @Test
    @DisplayName("Registo: Deve falhar se o email já existir")
    void testRegisterDuplicateEmail() {
        // Configurar comportamento: Email já existe
        when(mockUserDAO.emailExists("duplicate@test.com")).thenReturn(true);

        boolean result = authService.register("user", "pass", "duplicate@test.com");

        assertFalse(result);
        // Verifica que o createUser NUNCA foi chamado (poupança de recursos)
        verify(mockUserDAO, never()).createUser(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Registo: Deve ter sucesso com dados novos")
    void testRegisterSuccess() {
        when(mockUserDAO.emailExists("new@test.com")).thenReturn(false);
        // Simula que a criação retornou um ID válido (ex: 1)
        when(mockUserDAO.createUser(eq("newuser"), eq("new@test.com"), anyString())).thenReturn(1L);

        boolean result = authService.register("newuser", "pass", "new@test.com");

        assertTrue(result);
    }

    @Test
    @DisplayName("Login: Deve retornar username com credenciais certas")
    void testLoginSuccess() {
        // Simula que a BD encontrou o utilizador e retornou o nome
        when(mockUserDAO.authenticate(eq("valid@test.com"), anyString())).thenReturn("António");

        String result = authService.login("valid@test.com", "pass");

        assertEquals("António", result);
    }

    @Test
    @DisplayName("Login: Deve falhar se a DAO retornar null")
    void testLoginFailure() {
        when(mockUserDAO.authenticate(anyString(), anyString())).thenReturn(null);

        String result = authService.login("wrong@test.com", "wrongpass");

        assertNull(result);
    }

    @Test
    void testRegistration() {
        when(mockUserDAO.emailExists("new@test.com")).thenReturn(false);
        when(mockUserDAO.createUser(eq("NewUser"), eq("new@test.com"), anyString())).thenReturn(1L);

        assertTrue(authService.register("NewUser", "pass", "new@test.com"));
    }

    @Test
    @DisplayName("Login: Deve falhar com password errada")
    void testLoginWrongPassword() {
        // Configura o Mock para devolver o hash correto para este email
        // Aqui simulamos que a autenticação na BD falhou (retornou null)
        when(mockUserDAO.authenticate(anyString(), anyString())).thenReturn(null);

        String result = authService.login("valid@test.com", "WRONG_PASSWORD");

        assertNull(result, "O login deve retornar null se a password estiver errada");
    }

    @Test
    @DisplayName("Registo: Deve falhar se houver erro na base de dados (ex: ID inválido)")
    void testRegisterDatabaseFailure() {
        when(mockUserDAO.emailExists(anyString())).thenReturn(false);
        // Simula que o DAO falhou a criar o user (retornou -1)
        when(mockUserDAO.createUser(anyString(), anyString(), anyString())).thenReturn(-1L);

        boolean result = authService.register("user", "pass", "email@test.com");

        assertFalse(result, "O registo deve falhar se o DAO retornar -1");
    }
}
