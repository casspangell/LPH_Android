package org.lovepeaceharmony.androidapp.model

/**
 * Repeat
 * Created by Naveen Kumar M on 30/11/17.
 */

class Repeat {

    /*id used for distinguish days */
    var id: Int = 0

    /*alarmId is used for pendingIntentId*/
    var alarmId: Int = 0

    lateinit var shortName: String
    var isChecked: Boolean = false

    constructor()

    constructor(repeat: Repeat) {
        this.id = repeat.id
        this.alarmId = repeat.alarmId
        this.shortName = repeat.shortName
        this.isChecked = repeat.isChecked
    }

}
