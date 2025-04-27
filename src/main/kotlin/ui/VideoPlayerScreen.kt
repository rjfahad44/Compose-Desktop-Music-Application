package ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.launch
import util.demoVideoUrlList
import video_player.VideoPlayer


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayerScreen(
    windowState: WindowState
) {
    val coroutineScope = rememberCoroutineScope()
    val redditDataList by remember { mutableStateOf(demoVideoUrlList) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { redditDataList.size })
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
            .background(Color(0xFF121212)),
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
            val data = redditDataList[pageIndex]
            println("VideoPlayerScreen: $data")
            VideoPlayer(
                url = data,
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
            Text(
                text = "${pagerState.currentPage} / ${pagerState.pageCount}",
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
            )
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