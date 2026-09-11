import re

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

target = """        composable(
            "user_management?filter={filter}",
            arguments = listOf(navArgument("filter") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter")
            UserManagementScreen(navController = navController, authViewModel = authViewModel, initialFilter = filter)
        }
            UserManagementScreen(navController = navController, authViewModel = authViewModel)
        }"""

replacement = """        composable(
            "user_management?filter={filter}",
            arguments = listOf(navArgument("filter") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter")
            UserManagementScreen(navController = navController, authViewModel = authViewModel, initialFilter = filter)
        }"""

content = content.replace(target, replacement)
with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
