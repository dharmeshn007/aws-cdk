package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.codecommit.*;
import software.amazon.awscdk.services.codepipeline.*;
import software.amazon.awscdk.services.codepipeline.actions.*;
import java.util.*;
import static software.amazon.awscdk.services.codebuild.LinuxBuildImage.AMAZON_LINUX_2;
import  software.amazon.awscdk.SecretValue;

public class PipelineStack extends Stack {
    public PipelineStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public PipelineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Bucket artifactsBucket = new Bucket(this, "ArtifactsBucket");

        IRepository codeRepo = Repository.fromRepositoryName(this, "AppRepository", "sam-app");

    Pipeline pipeline = new Pipeline(this, "Pipeline", PipelineProps.builder()
        .artifactBucket(artifactsBucket).build());

    Artifact sourceOutput = new Artifact("sourceOutput");

    GitHubSourceAction sourceAction = GitHubSourceAction.Builder.create()
                .actionName("GitHub_Source")
                .owner("dharmeshn007")
                .repo("aws-cdk")
                .branch("main")
                .oauthToken(SecretValue.secretsManager("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"))
                .output(sourceOutput)
                .build();

    pipeline.addStage(StageOptions.builder()
        .stageName("Source")
        .actions(Collections.singletonList(sourceAction))
        .build());

       

    // Declare build output as artifacts
Artifact buildOutput = new Artifact("buildOutput");

// Declare a new CodeBuild project
PipelineProject buildProject = new PipelineProject(this, "Build", PipelineProjectProps.builder()
        .environment(BuildEnvironment.builder()
                .buildImage(AMAZON_LINUX_2).build())
        .environmentVariables(Collections.singletonMap("PACKAGE_BUCKET", BuildEnvironmentVariable.builder()
                .value(artifactsBucket.getBucketName())
                .build()))
        .build());

// Add the build stage to our pipeline
CodeBuildAction buildAction = new CodeBuildAction(CodeBuildActionProps.builder()
        .actionName("Build")
        .project(buildProject)
        .input(sourceOutput)
        .outputs(Collections.singletonList(buildOutput))
        .build());

pipeline.addStage(StageOptions.builder()
        .stageName("Build")
        .actions(Collections.singletonList(buildAction))
        .build());
    }
}