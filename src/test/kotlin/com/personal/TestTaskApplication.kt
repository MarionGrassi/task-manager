package com.personal

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<TaskApplication>().with(TestcontainersConfiguration::class).run(*args)
}
