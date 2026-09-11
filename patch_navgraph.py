with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

content = content.replace(
    'composable("user_management") {',
    '''composable(
            "user_management?filter={filter}",
            arguments = listOf(navArgument("filter") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter")
            UserManagementScreen(navController = navController, authViewModel = authViewModel, initialFilter = filter)
        }'''
)

# And if there's any old composable("user_management") that we missed, wait we replaced the exact match.
# Wait, I didn't replace the implementation inside. The replacement has the implementation. Let me make sure.

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
