package com.abduqodirov.invitex

class MembersManager {
    companion object {
        var members = mutableMapOf<String, Int>()
        var membersCollapsed = mutableMapOf<String, Boolean>()
    }
}