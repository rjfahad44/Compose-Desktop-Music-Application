package ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(windowState: WindowState) {
    val coroutineScope = rememberCoroutineScope()
    val tabItems = remember { arrayListOf("Audio Player", "Video Player") }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabItems.size })

    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
        snapAnimationSpec = SpringSpec(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            flingBehavior = flingBehavior,
            key = { index -> index }
        ) {
            when (it) {
                0 -> AudioPlayerScreen(windowState)
                1 -> VideoPlayerScreen(windowState)
            }
        }

        // Get window information
        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val windowWidthDp: Dp = with(density) { windowInfo.containerSize.width.toDp() }
        val edge = windowWidthDp.div(2).minus(100.dp)

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            backgroundColor = Color.Transparent,
            divider = {},
            modifier = Modifier.padding(top = 8.dp),
            indicator = { tabPositions ->
                val modifier = Modifier
                    .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                    .padding(horizontal = 38.dp)

                TabRowDefaults.Indicator(
                    modifier, color = White
                )
            },
            edgePadding = edge
        ) {
            tabItems.forEachIndexed { index, item ->
                val isSelected = pagerState.currentPage == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        val textStyle = if (isSelected) {
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = White
                            )
                        }
                        else {
                            TextStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = White.copy(alpha = 0.6f)
                            )
                        }
                        Text(text = item, style = textStyle)
                    }
                )
            }
        }
    }
}
