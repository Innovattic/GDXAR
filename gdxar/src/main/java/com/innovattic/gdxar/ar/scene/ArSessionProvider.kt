package com.innovattic.gdxar.ar.scene

import com.google.ar.core.Session

interface ArSessionProvider {
    fun getSession(): Session?
}
