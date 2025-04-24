package ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.launch
import util.demoVideoUrlList
import video_player.VideoPlayer


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayerScreen(pagerState: PagerState, windowState: WindowState) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { demoVideoUrlList.size })
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
        snapAnimationSpec = SpringSpec(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionDown -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                            true
                        }

                        Key.DirectionUp -> {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                            true
                        }

                        else -> false
                    }
                } else false
            }
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        val event = awaitPointerEvent()
                        val scrollDeltaY = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f

                        if (scrollDeltaY > 0f && pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        } else if (scrollDeltaY < 0f && pagerState.currentPage < demoVideoUrlList.lastIndex) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                }
            }

    ) {
        VerticalPager(
            state = pagerState,
            flingBehavior = flingBehavior,
            beyondBoundsPageCount = 1,
            userScrollEnabled = true,
            modifier = Modifier.fillMaxSize()
                .background(Color(0xFF121212))
                .align(Alignment.Center)
                .padding(top = 70.dp)
        ) { pageIndex ->
            val url = demoVideoUrlList[pageIndex]
            VideoPlayer(
                url = url,
                pagerState = pagerState,
                pageIndex = pageIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121212))
            )
        }


        Column(
            modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd)
        ) {
            // Previous Page
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                modifier = Modifier.rotate(90f)
            ) {
                Icon(
                    painterResource("images/ic_previous.xml"),
                    contentDescription = "Previous Page",
                    tint = Color.White
                )
            }

            // Next Page
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.rotate(90f)
            ) {
                Icon(
                    painterResource("images/ic_next.xml"),
                    contentDescription = "Next Page",
                    tint = Color.White
                )
            }
        }

    }
}