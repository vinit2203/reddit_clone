package com.example.redditclone.repository;

import com.example.redditclone.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubredditRepository extends JpaRepository<Subreddit,Long> {
    Subreddit findByName(String subredditName);
}
