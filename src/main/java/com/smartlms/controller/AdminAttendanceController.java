
package com.smartlms.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartlms.entity.Attendance;
import com.smartlms.entity.AttendanceStatus;
import com.smartlms.entity.Course;
import com.smartlms.repository.AttendanceRepository;
import com.smartlms.repository.CourseRepository;

@Controller
@RequestMapping("/admin/attendance")
public class AdminAttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;

    public AdminAttendanceController(
            AttendanceRepository attendanceRepository,
            CourseRepository courseRepository) {

        this.attendanceRepository = attendanceRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public String attendanceReport(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Model model) {

        List<Course> courses = courseRepository.findAll();

        // Fix: Create an effectively final date variable
        final LocalDate selectedDate =
                (date == null) ? LocalDate.now() : date;

        List<Attendance> attendanceList;

        if (courseId != null) {

            attendanceList =
                    attendanceRepository.findByCourseIdAndAttendanceDate(
                            courseId, selectedDate);

        } else {

            attendanceList = attendanceRepository.findAll()
                    .stream()
                    .filter(a -> a.getAttendanceDate().equals(selectedDate))
                    .toList();
        }

        // Calculate attendance statistics
        long present = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absent = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long late = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        long total = attendanceList.size();

        double percentage = total > 0
                ? ((present + late) * 100.0) / total
                : 0.0;

        // Send data to Thymeleaf
        model.addAttribute("courses", courses);
        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedDate", selectedDate);

        model.addAttribute("totalAttendance", total);
        model.addAttribute("presentCount", present);
        model.addAttribute("absentCount", absent);
        model.addAttribute("lateCount", late);

        model.addAttribute("attendancePercentage",
                Math.round(percentage * 100.0) / 100.0);

        return "admin/attendance";
    }
}
