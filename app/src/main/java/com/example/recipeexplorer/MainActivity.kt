package com.example.recipeexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class Recipe(
    val id: Int,
    val title: String,
    val description: String,
    val details: String
)

data class RecipeUiState(
    val recipes: List<Recipe> = sampleRecipes,
    val selectedRecipe: Recipe? = null
)

val sampleRecipes = listOf(
    Recipe(
        id = 1,
        title = "Spaghetti Bolognese",
        description = "A classic Italian dish with rich meat sauce.",
        details = "Cook spaghetti noodles until tender. Prepare a sauce using ground beef, tomato sauce, garlic, onions, and Italian seasoning. Serve warm with parmesan cheese."
    ),
    Recipe(
        id = 2,
        title = "Chicken Curry",
        description = "A spicy and savory curry with tender chicken pieces.",
        details = "Cook chicken with curry powder, garlic, onions, coconut milk, and vegetables. Simmer until the sauce thickens and serve over rice."
    ),
    Recipe(
        id = 3,
        title = "Beef Stroganoff",
        description = "A creamy dish with sautéed pieces of beef and mushrooms.",
        details = "Brown strips of beef with mushrooms and onions. Add broth and sour cream to create a creamy sauce. Serve over noodles or rice."
    ),
    Recipe(
        id = 4,
        title = "Vegetable Stir Fry",
        description = "A colorful mix of vegetables cooked in a savory sauce.",
        details = "Stir fry broccoli, carrots, bell peppers, and snap peas. Add soy sauce, garlic, and ginger. Serve with rice or noodles."
    ),
    Recipe(
        id = 5,
        title = "Chocolate Chip Cookies",
        description = "Soft and sweet cookies filled with chocolate chips.",
        details = "Mix flour, sugar, butter, eggs, vanilla, and chocolate chips. Bake until golden brown and let cool before serving."
    )
)

class RecipeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState

    fun selectRecipe(recipeId: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedRecipe = currentState.recipes.find { it.id == recipeId }
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RecipeExplorerApp()
            }
        }
    }
}

@Composable
fun RecipeExplorerApp(recipeViewModel: RecipeViewModel = viewModel()) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val uiState by recipeViewModel.uiState.collectAsState()

    if (isTablet) {
        TabletRecipeLayout(
            uiState = uiState,
            onRecipeSelected = { recipeViewModel.selectRecipe(it) }
        )
    } else {
        PhoneRecipeNavigation(
            uiState = uiState,
            onRecipeSelected = { recipeViewModel.selectRecipe(it) }
        )
    }
}

@Composable
fun PhoneRecipeNavigation(
    uiState: RecipeUiState,
    onRecipeSelected: (Int) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "recipeList"
    ) {
        composable("recipeList") {
            RecipeListScreen(
                recipes = uiState.recipes,
                onRecipeClick = { recipeId ->
                    onRecipeSelected(recipeId)
                    navController.navigate("recipeDetail/$recipeId")
                }
            )
        }

        composable(
            route = "recipeDetail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId")
            val recipe = uiState.recipes.find { it.id == recipeId }

            RecipeDetailScreen(recipe = recipe)
        }
    }
}

@Composable
fun TabletRecipeLayout(
    uiState: RecipeUiState,
    onRecipeSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            RecipeListScreen(
                recipes = uiState.recipes,
                onRecipeClick = onRecipeSelected
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (uiState.selectedRecipe == null) {
                Text(
                    text = "Select a recipe to view details.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                RecipeDetailScreen(recipe = uiState.selectedRecipe)
            }
        }
    }
}

@Composable
fun RecipeListScreen(
    recipes: List<Recipe>,
    onRecipeClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Recipe List",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) }
                )
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun RecipeDetailScreen(recipe: Recipe?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (recipe == null) {
            Text(
                text = "Recipe not found.",
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = recipe.details,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}