package com.mohdroid.domain.game.session

import com.mohdroid.domain.game.model.level.Difficulty

/**
 * Identity token issued before a level load. Only its issuing session may activate it.
 * Deliberately not a data class: equal difficulties must still represent distinct requests.
 */
internal class GameStartRequest(val difficulty: Difficulty)
