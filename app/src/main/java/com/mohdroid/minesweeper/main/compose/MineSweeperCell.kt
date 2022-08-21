package com.mohdroid.minesweeper.main.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mohdroid.algorithm.MissionCoordinator
import com.mohdroid.algorithm.MissionCoordinator.PlayerState.*
import com.mohdroid.algorithm.item.Item
import com.mohdroid.algorithm.item.assets.*
import com.mohdroid.minesweeper.R


@Composable
fun getPainter(item: Item, state: MissionCoordinator.PlayerState): Painter {
    return when (item) {
        is SoldierAsset -> {
            when (state) {
                WIN ->
                    painterResource(id = R.drawable.ic_cell_happy_s)
                LOOSE -> painterResource(id = R.drawable.ic_cell_sad_s)
                else -> painterResource(id = R.drawable.ic_cell_soldier)
            }
        }
        is StartAsset -> {
            if (item.isSteppedOn) {
                painterResource(id = R.drawable.ic_cell_walked)
            } else
                painterResource(id = R.drawable.ic_cell_soldier)
        }
        is ExitAsset -> {
            if (item.isBlock)
                painterResource(id = R.drawable.ic_cell_close_door)
            else painterResource(id = R.drawable.ic_cell_open_door)
        }
        is MineAsset -> {
            painterResource(id = R.drawable.ic_cell_mine)
        }
        is KeyAsset -> {
            if (item.isSteppedOn) {
                painterResource(id = R.drawable.ic_cell_key_founded)
            } else {
                painterResource(id = R.drawable.ic_cell_key)
            }
        }
        is BlockAsset -> {
            painterResource(id = R.drawable.ic_cell_block)
        }
        else -> {
            if (item.isSteppedOn) {
                painterResource(id = R.drawable.ic_cell_walked)
            } else
                painterResource(id = R.drawable.ic_cell__2_)
        }
    }
}

@Composable
fun MineSweeperCell(
    modifier: Modifier = Modifier,
    item: Item,
    state: MissionCoordinator.PlayerState
) {
    Box(
        modifier = modifier.padding(0.2.dp)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = getPainter(item = item, state),
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )
    }

}
