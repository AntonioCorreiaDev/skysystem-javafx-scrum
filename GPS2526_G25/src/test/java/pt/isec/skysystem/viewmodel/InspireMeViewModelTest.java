package pt.isec.skysystem.viewmodel;

import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.isec.skysystem.model.data.Suggestion;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InspireMeViewModelTest {

    private InspireMeViewModel viewModel;

    @BeforeEach
    void setUp() {
        // Inicializa o ViewModel
        viewModel = new InspireMeViewModel();
    }

    @Test
    @DisplayName("Tags: Deve extrair, separar e ordenar tags únicas")
    void testGetAllUniqueTags() {
        // acede á lista de sugestoes (que é Observable)
        ObservableList<Suggestion> list = viewModel.getSuggestionsList();

        // limpa dados reais e insere dados de teste
        list.clear();

        // adiciona sugestão 1: "praia, sol"
        list.add(new Suggestion("Algarve", "praia, sol", "Razão", "100", "img.jpg"));

        // adiciona sugestão 2: "história,  sol " (com espaços extra e tag repetida 'sol')
        list.add(new Suggestion("Lisboa", "história,  sol ", "Razão", "150", "img.jpg"));

        // adiciona sugestão 3: "gastronomia"
        list.add(new Suggestion("Porto", "gastronomia", "Razão", "120", "img.jpg"));

        // executa metodo
        List<String> tags = viewModel.getAllUniqueTags();

        //verificacoes

        assertEquals(4, tags.size(), "Devem existir 4 tags únicas");

        assertEquals("gastronomia", tags.get(0));
        assertEquals("história", tags.get(1));
        assertEquals("praia", tags.get(2));
        assertEquals("sol", tags.get(3));

        // nao devem ter espacos em branco
        assertFalse(tags.contains("  sol "), "As tags devem vir sem espaços extra (trim)");
    }

    @Test
    @DisplayName("Tags: Deve lidar com lista vazia")
    void testEmptyTags() {
        viewModel.getSuggestionsList().clear();
        List<String> tags = viewModel.getAllUniqueTags();
        assertTrue(tags.isEmpty(), "Se não há sugestões, não deve haver tags");
    }
}