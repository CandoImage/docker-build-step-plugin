package org.jenkinsci.plugins.dockerbuildstep.cmd;

import java.util.Arrays;
import java.util.List;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

import org.jenkinsci.plugins.dockerbuildstep.action.EnvInvisibleAction;
import org.kohsuke.stapler.DataBoundConstructor;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.model.ContainerInspectResponse;

/**
 * This command starts one or more Docker containers. It also exports some build environment variable like IP or started
 * containers.
 * 
 * @see http://docs.docker.io/en/master/api/docker_remote_api_v1.8/#start-a-container
 * 
 * @author vjuranek
 * 
 */
public class StartCommand extends DockerCommand {

    private String containerIds;

    @DataBoundConstructor
    public StartCommand(String containerIds) {
        this.containerIds = containerIds;
    }

    public String getContainerIds() {
        return containerIds;
    }

    @Override
    public void execute(@SuppressWarnings("rawtypes") AbstractBuild build, BuildListener listener)
            throws DockerException {
        if (containerIds == null || containerIds.isEmpty()) {
            throw new IllegalArgumentException("At least one parameter is required");
        }

        List<String> ids = Arrays.asList(containerIds.split(","));
        DockerClient client = getClient();
        for (String id : ids) {
            id = id.trim();
            client.startContainer(id);

            ContainerInspectResponse inspectResp = client.inspectContainer(id);
            EnvInvisibleAction envAction = new EnvInvisibleAction(inspectResp);
            build.addAction(envAction);
        }
    }

    @Extension
    public static class StartCommandDescriptor extends DockerCommandDescriptor {
        @Override
        public String getDisplayName() {
            return "Start constainer(s)";
        }
    }

}
