package com.example.redditclone.mapper;

import com.example.redditclone.dto.*;
import com.example.redditclone.model.*;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubredditMapper {

    @Mapping(target = "numberOfPosts", expression = "java(mapPosts(subreddit.getPosts()))")
    SubredditDto mapsubredditToDto(Subreddit subreddit);

    default Integer mapPosts(List<Post> numberOfPosts){
        return numberOfPosts.size();
    }

    @InheritInverseConfiguration
    @Mapping(target = "posts" , ignore = true)
    Subreddit mapDtoToSubreddit(SubredditDto subredditdto);
}
