package tt.info.paulrytaylor.LocationPublisher.models

import kotlin.math.max
import kotlin.math.min

class ClientModel {
    var min: Double = 0.0
    var max: Double = 0.0
    var total: Double = 0.0
    var count: Int = 0
    var student_id: String = ""

    constructor(studentID: String){
        student_id = studentID
    }
    constructor(studentID: String, speed: Double){
        student_id = studentID
        add(speed)
    }
    fun add(speed: Double){
        if(count==0){
            min = speed
            max = speed
        }
        else{
            max = max(max,speed)
            min = min(min,speed)
        }
        count++
        total += speed
    }
    fun average(): Double {
        return total/count
    }
}