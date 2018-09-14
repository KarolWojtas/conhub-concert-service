package com.karol.repositories

import com.karol.domain.Concert
import com.karol.domain.Venue
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataMongoTest
class RepositoryTest{
    @Autowired lateinit var mongoTemplate: MongoTemplate
    @Autowired lateinit var concertRepository: ConcertRepository
    @Autowired lateinit var venueRepository: VenueRepository
    val venue = Venue(id = null, name = "Arena")
    val concert = Concert(venue = venue, name="Concert")
    @Before
    fun setUp() {
        mongoTemplate.save(venue)
        mongoTemplate.save(concert)
    }
    @Test
    fun `test dependencies injected`(){
        Assert.assertNotNull(mongoTemplate)
        Assert.assertNotNull(concertRepository)
        Assert.assertNotNull(venueRepository)
    }
}