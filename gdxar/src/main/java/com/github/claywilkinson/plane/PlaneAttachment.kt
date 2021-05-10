/*
Copyright 2017 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.github.claywilkinson.plane

import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState

/**
 * Attaches an object of type [T] to a plane using an anchor.
 * This associates the object both with a detected plane and an ARCore anchor in space.
 * This keeps the object oriented consistently with the anchor and the plane.
 */
open class PlaneAttachment<T>(private val plane: Plane, val anchor: Anchor, val data: T) {

    // Allocate temporary storage to avoid multiple allocations per frame.
    private val mPoseTranslation = FloatArray(3)
    private val mPoseRotation = FloatArray(4)

    val isTracking: Boolean
        get() = plane.trackingState == TrackingState.TRACKING &&
            anchor.trackingState == TrackingState.TRACKING

    val pose: Pose
        get() {
            val pose = anchor.pose
            pose.getTranslation(mPoseTranslation, 0)
            pose.getRotationQuaternion(mPoseRotation, 0)
            mPoseTranslation[1] = plane.centerPose.ty()
            return Pose(mPoseTranslation, mPoseRotation)
        }
}
