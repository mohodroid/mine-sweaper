package com.mohdroid.minesweeper.feature.level.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.mohdroid.compose.theme.MsTheme
import com.mohdroid.compose.theme.bottomStyle
import com.mohdroid.minesweeper.R
import com.mohdroid.minesweeper.feature.level.model.LevelItemUiModel
import com.mohdroid.minesweeper.ui.component.IllustratedButton

@Composable
internal fun LevelButton(
    level: LevelItemUiModel,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IllustratedButton(modifier = modifier, enable = enabled, onClicked = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(level.titleResource), style = MsTheme.typography.bottomStyle)
            Text(
                stringResource(R.string.level_description, level.rows, level.columns, level.mines, level.keys),
                fontSize = 12.sp
            )
        }
    }
}
