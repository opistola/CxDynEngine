package com.checkmarx.engine.aws;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class AwsUtils {
	
	private AwsUtils() {
		// static class
	}
	
	public static String getName(@NotNull Instance instance) {
		return getTag(instance, AwsConstants.NAME_TAG);
	}

	public static String getTag(@NotNull Instance instance, String key) {
		final List<Tag> tags = instance.getTags();
		if (tags == null) return null;
		
		final Tag theTag = Iterables.find(tags, new Predicate<Tag>() {
		    @Override
		    public boolean apply(final Tag tag) {
		        return tag.getKey().equals(key);
		    }
		});
		return theTag == null ? null : theTag.getValue();
	}
	
	public static String printInstanceDetails(@NotNull Instance instance) {
		final String tags = printTags(instance.getTags(), false);
		return MoreObjects.toStringHelper(instance)
				.add("name", getName(instance))
				.add("state", instance.getState().getName().toUpperCase())
				.add("instanceId", instance.getInstanceId())
				.add("instanceType", instance.getInstanceType())
				.add("imageId", instance.getImageId())
				.add("privateIp", instance.getPrivateIpAddress())
				.add("publicIp", instance.getPublicIpAddress())
				.add("tags", "[" + tags + "]")
				.toString();
	}
	
	public static String printTags(@NotNull List<Tag> tags, boolean includeName) {
		final StringBuilder sb = new StringBuilder();
		for (Tag tag : tags) {
	    	final String key = tag.getKey();
	    	if (!includeName && AwsConstants.NAME_TAG.equals(key)) continue;
			sb.append(String.format("%s=%s; ", key, tag.getValue()));
		}
		return sb.toString().replaceAll("; $", "");
	}

}
