package org.itmdt.bookmarks;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.icegreen.greenmail.spring.GreenMailBean;
import org.itmdt.bookmarks.groupuser.GroupUserUpdateDTO;
import org.itmdt.bookmarks.groupuser.GroupUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
public class BookmarksApplication {

	@Autowired
	private PageSerializer pageSerializer;

	public static void main(String[] args) {
		SpringApplication.run(BookmarksApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setSkipNullEnabled(true);

		// set up skipped fields
		modelMapper.addMappings(new PropertyMap<GroupUserUpdateDTO, GroupUser>() {
			@Override
			protected void configure() {
				skip(destination.getGroupUserId());
			}
		});

		return modelMapper;
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer addPageSerialization() {
		return new Jackson2ObjectMapperBuilderCustomizer() {
			@Override
			public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
				jacksonObjectMapperBuilder.serializerByType(Page.class, pageSerializer);
			}
		};
	}
}
