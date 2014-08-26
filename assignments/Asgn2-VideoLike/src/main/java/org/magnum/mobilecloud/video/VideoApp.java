/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.collect.Lists;

@Controller
public class VideoApp {
	
	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */

	@Autowired
	private VideoRepository videos;
	
    
    
    @RequestMapping(value="/video", method=RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideoList() {
    	return Lists.newArrayList(videos.findAll());
    }
    
    @RequestMapping(value="/video/{id}", method=RequestMethod.GET)
    public @ResponseBody Video getVideo(@PathVariable("id") long id) {
    	return videos.findOne(id);
    }
    
    @RequestMapping(value="/video/{id}/likedby", method=RequestMethod.GET)
    public @ResponseBody ArrayList<String> getLikedBy(@PathVariable("id") long id) {
    	return Lists.newArrayList(videos.findOne(id).getLikedby());
    }
    
    @RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByName(
			// Tell Spring to use the "title" parameter in the HTTP request's query
			// string as the value for the title method parameter
			@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title
	){
		return videos.findByName(title);
	}
	
    
    @RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(
			@RequestParam(VideoSvcApi.DURATION_PARAMETER) long maxduration
	){
		return videos.findByDurationLessThan(maxduration);
	}
    
    
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){
		v.setLikes(0);
		return videos.save(v);
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/like", method=RequestMethod.POST)
	public ResponseEntity<String> likeVideo(@PathVariable("id") long id, Principal p){
		if(videos.exists(id)){
		Video v=videos.findOne(id);
		String name = p.getName();
		
		if(v.getLikedby().add(name)){
			v.setLikes(v.getLikes()+1);
			videos.save(v);
			return new ResponseEntity<String>(HttpStatus.OK);
		}else{
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);	
		}
		 
		} else {
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/unlike", method=RequestMethod.POST)
	public ResponseEntity<String> unlikeVideo(@PathVariable("id") long id, Principal p){
		if(videos.exists(id)){
		Video v=videos.findOne(id);
		String name = p.getName();
		if(v.getLikedby().remove(name)){
			v.setLikes(v.getLikes()-1);
			videos.save(v);
			return new ResponseEntity<String>(HttpStatus.OK);
		}else{
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);	
		}
		
		} else{
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		}
	}
	
	
	private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId;
        return url;
    }

    private String getUrlBaseForLocalServer() {
       HttpServletRequest request = 
           ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       String base = 
          "http://"+request.getServerName() 
          + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
       return base;
    }
	
}
