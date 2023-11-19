package com.forthreal.mapper

import com.forthreal.domain.User
import org.apache.ibatis.annotations.*

@Mapper
interface UserMapper {
    @Insert("CREATE TABLE Users (" +
            "id INT(5) NOT NULL AUTO_INCREMENT, " +
            "username VARCHAR(100) NOT NULL, " +
            "password VARCHAR(200) NOT NULL, " +
            "PRIMARY KEY(id), " +
            "CONSTRAINT unique_username UNIQUE(username))")
    fun createTable()

    @Select("SELECT * FROM Users WHERE username = #{username}")
    fun findByUsername(@Param("username") username: String): User?

    @Update("UPDATE Users SET password = #{password} WHERE username = #{username}")
    fun updatePassword(user: User)

    @Delete("DELETE FROM Users WHERE username = #{username}")
    fun deleteUser(@Param("username") username: String)

    @Insert("INSERT INTO Users(username,password) VALUES(#{username},#{password})")
    fun addUser(user: User)
}