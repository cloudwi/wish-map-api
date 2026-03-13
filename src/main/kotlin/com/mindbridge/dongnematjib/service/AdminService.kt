package com.mindbridge.dongnematjib.service

import com.mindbridge.dongnematjib.domain.restaurant.RestaurantStatus
import com.mindbridge.dongnematjib.dto.RestaurantListResponse
import com.mindbridge.dongnematjib.dto.toListResponse
import com.mindbridge.dongnematjib.exception.ResourceNotFoundException
import com.mindbridge.dongnematjib.repository.LikeRepository
import com.mindbridge.dongnematjib.repository.RestaurantRepository
import com.mindbridge.dongnematjib.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository,
    private val likeRepository: LikeRepository
) {

    @Transactional(readOnly = true)
    fun getPendingRestaurants(pageable: Pageable): Page<RestaurantListResponse> {
        return restaurantRepository.findByStatus(RestaurantStatus.PENDING, pageable)
            .map { restaurant ->
                val likeCount = likeRepository.countByRestaurant(restaurant)
                restaurant.toListResponse(likeCount)
            }
    }

    @Transactional
    fun approveRestaurant(restaurantId: Long, adminId: Long) {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }
        val admin = userRepository.findById(adminId)
            .orElseThrow { ResourceNotFoundException("Admin not found: $adminId") }

        if (restaurant.status != RestaurantStatus.PENDING) {
            throw IllegalArgumentException("이미 처리된 제안입니다")
        }

        restaurant.status = RestaurantStatus.APPROVED
        restaurant.approvedBy = admin
    }

    @Transactional
    fun rejectRestaurant(restaurantId: Long, adminId: Long) {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }
        userRepository.findById(adminId)
            .orElseThrow { ResourceNotFoundException("Admin not found: $adminId") }

        if (restaurant.status != RestaurantStatus.PENDING) {
            throw IllegalArgumentException("이미 처리된 제안입니다")
        }

        restaurant.status = RestaurantStatus.REJECTED
    }
}
