package org.fsalah.config

import java.util.concurrent.atomic.AtomicBoolean

object Fault {
    var isFault = AtomicBoolean(false)
}