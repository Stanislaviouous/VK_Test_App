package com.example.audiorec.list

class AudioRecord {
    lateinit var name: String // Название
    lateinit var date: String // Дата создания
    lateinit var duration: String // Продолжительность
    lateinit var path: String // Путь к файлу
    var timestamp: Long = 0 // Отметка

    constructor(name: String, date: String, duration: String, path: String, timestamp: Long) {
        this.name = name
        this.date = date
        this.duration = duration
        this.path = path
        this.timestamp = timestamp
    }
}