package pt.isec.skysystem.model.data.dao;

import org.junit.jupiter.api.*;
import pt.isec.skysystem.model.data.DataBaseManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {

    private UserDAO userDAO;
    private static final String TEST_DB_PATH = "database/skysystem_test.db";

    @BeforeEach
    void setUp() throws Exception {
        // 1. Dizer ao Manager para usar a BD de teste
        DataBaseManager.setTestMode(true);

        // 2. Apagar apenas a BD de teste antiga
        Files.deleteIfExists(Paths.get(TEST_DB_PATH));

        // 3. Inicializar a BD de teste (Tabelas vazias)
        DataBaseManager.initializeDatabase();

        userDAO = new UserDAO();
    }

    @Test
    @DisplayName("Deve criar utilizador e retornar um ID válido")
    void testCreateUser() {
        long id = userDAO.createUser("TesteUser", "teste@email.com", "hash123");

        assertTrue(id > 0, "O ID do utilizador criado deve ser maior que 0");
    }

    @Test
    @DisplayName("Deve autenticar utilizador corretamente")
    void testAuthenticate() {
        // Inserir
        userDAO.createUser("LoginUser", "login@email.com", "senhaSeguraHash");

        // Tentar Login Correto
        String username = userDAO.authenticate("login@email.com", "senhaSeguraHash");
        assertEquals("LoginUser", username);

        // Tentar Login Incorreto (Password errada)
        String fail = userDAO.authenticate("login@email.com", "senhaErrada");
        assertNull(fail);
    }

    @Test
    @DisplayName("Deve detetar email duplicado")
    void testEmailExists() {
        userDAO.createUser("User1", "unico@email.com", "hash");

        assertTrue(userDAO.emailExists("unico@email.com"));
        assertFalse(userDAO.emailExists("naoexiste@email.com"));
    }

    // --- testInsertUser ---
    @Test
    void testInsertUser() {
        long id = userDAO.createUser("DaoTester", "dao@test.com", "hashedPass");
        assertTrue(id > 0, "Deve retornar um ID válido (SQL INSERT funcionou)");
    }

    // --- testGetUser (via Authenticate ou EmailExists) ---
    @Test
    void testGetUser() {
        userDAO.createUser("DaoTester", "dao@test.com", "hashedPass");

        // Verifica recuperação por credenciais
        String username = userDAO.authenticate("dao@test.com", "hashedPass");
        assertEquals("DaoTester", username);

        // Verifica verificação de existência
        assertTrue(userDAO.emailExists("dao@test.com"));
    }

    @AfterAll
    static void tearDownAll() {
        // Opcional: Voltar ao modo normal no fim de todos os testes
        DataBaseManager.setTestMode(false);
    }
}