package ru.skillbranch.kotlinexample

import ru.skillbranch.kotlinexample.User
import ru.skillbranch.kotlinexample.User.Factory.fullNameToPair

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        return User.makeUser(fullName, email = email, password = password)
            .also { user -> map[user.login] = user }
    }

    fun registerUserByPhone(fullName: String, rawPhone: String) : User {
        if (!isPhoneValid(rawPhone))
            throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")

        if (map.contains(getPhoneNumber(rawPhone)))
            throw  IllegalArgumentException("A user with this phone already exists")

        return User.makeUser(fullName,null,null, phone = rawPhone).also { map[it.login] = it }
    }

    fun requestAccessCode(login: String) : Unit {
        var key = login.trim()
        if (!key.contains("@"))
            key = getPhoneNumber(key)
        map[key]?.generateNewAuthCode()
    }

    fun loginUser(login: String, password: String): String? {
        return map[login.trim()]?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun importUsers(list: List<String>): List<User> {
        var users = mutableListOf<User>()
        list.forEach {
            var line = it.trim()
            if (line.isNotEmpty()) {
                var user = User.makeUserFromCSV(line)
                users.add(user)
            }
        }
        return users.toList()
    }

    private fun getPhoneNumber(rawPhone: String) = rawPhone.replace("[^+\\d]".toRegex(), "")

    private fun isPhoneValid(rawPhone: String): Boolean = getPhoneNumber(rawPhone).matches("[+]\\d{11}".toRegex())
}