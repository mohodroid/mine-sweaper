package com.mohdroid.minesweeper.feature.game.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohdroid.compose.theme.MsTypography
import com.mohdroid.minesweeper.feature.game.model.GameBoardUiModel

@Composable
internal fun GameStats(game: GameBoardUiModel) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Mines: ${game.stats.mines}", style = MsTypography.body1)
            Text("Keys: ${game.stats.foundKeys}/${game.stats.totalKeys}", style = MsTypography.body1)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Lives: ${game.stats.remainingLives}/${game.stats.startingLives}", style = MsTypography.body1)
            Text("Moves: ${game.stats.moves}", style = MsTypography.body1)
        }
    }
}
