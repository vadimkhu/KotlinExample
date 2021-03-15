package ru.skillbranch.kotlinexample

object UserHolder {
    private val users = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        if (users.contains(email.toLowerCase()))
            throw IllegalArgumentException("A user with this email already exists")
        else return User.makeUser(fullName,email=email, password = password)
            .also { user -> users[user.login] = user }    }

    fun registerUserByPhone(fullName: String, rawPhone: String) : User {
        if (users.contains(getPhoneNumber(rawPhone)))
            throw  IllegalArgumentException("A user with this phone already exists")

        if (!isPhoneValid(rawPhone))
            throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")

        return User.makeUser(fullName,null,null, rawPhone).also { users[it.login] = it }
    }

    fun requestAccessCode(login: String) : Unit {
        users[getLogin(login)]?.requestAccessCode()
    }

    fun loginUser(login: String, password: String): String? {
        return users[getLogin(login)]?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun importUsers(list: List<String>): List<User> {
        return list.map {
            println(it)
            parseCsvLine(it)
        }.toList()
    }

    fun parseCsvLine(line: String) : User {
        val info = line.split(";").map { item -> if (item.isNotEmpty()) item else "" }
        if (info.size != 5)
            throw java.lang.IllegalArgumentException("Incorrect user data in a CSV line")
        val fname = info[0].trim()
        val salt = info[2].split(":")[0].trim()
        val hash = info[2].split(":")[1].trim()
        val mail: String? = if (info[1].isNullOrEmpty()) null else info[1].trim()
        val phone: String? = if (info[3].isNullOrEmpty()) null else info[3].trim()
        return User.makeUser(
            fname,
            mail,
            null,
            phone,
            hash,
            salt)
    }

    private fun getPhoneNumber(rawPhone: String) = rawPhone.replace("[^+\\d]".toRegex(), "")

    private fun isPhoneValid(rawPhone: String): Boolean = getPhoneNumber(rawPhone).matches("[+]\\d{11}".toRegex())

    private fun getLogin(login: String) : String =
        if (login.contains("@"))
            login.trim()
        else
            getPhoneNumber(login)
}