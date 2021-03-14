package ru.skillbranch.kotlinexample

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

    fun loginUser(login: String, password: String): String? {
        return map[login.trim()]?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun importUsers(list: List<String>): List<User> {
        var users = mutableListOf<User>()
        list.forEach {
            var user = User.makeUserFromCSV(it)
            users.add(user)
        }
        return users.toList()
    }
}