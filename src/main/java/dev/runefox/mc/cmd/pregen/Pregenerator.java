package dev.runefox.mc.cmd.pregen;

import com.mojang.datafixers.util.Either;
import dev.runefox.mc.cmd.CommandsMod;
import dev.runefox.mc.cmd.ServerLevelAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Pregenerator {
    private static final int CHUNKS_PER_BATCH = Runtime.getRuntime().availableProcessors() * 2;
    private static final int UPDATE_INTERVAL = 10;

    private final ServerLevel level;
    private final PositionIterator iterator;
    private final PregenListener listener;
    private final MinecraftServer server;

    private final ServerChunkCache chunkSrc;
    private final ChunkMap chunkMap;

    private final AtomicInteger currentBatch = new AtomicInteger(0);
    private final AtomicInteger totalOk = new AtomicInteger(0);
    private final AtomicInteger totalFail = new AtomicInteger(0);
    private final int total;

    private boolean stop;
    private int intervalCounter = 0;

    public Pregenerator(ServerLevel level, PositionIterator iterator, PregenListener listener) {
        this.level = level;
        this.iterator = iterator;
        this.listener = listener;

        this.server = level.getServer();

        this.chunkSrc = level.getChunkSource();
        this.chunkMap = chunkSrc.chunkMap;

        this.total = iterator.total();

        ((ServerLevelAccess) level).rfx_cmd_pregenerator(this);
    }

    public PregenStatus status() {
        return new PregenStatus(totalOk.get(), totalFail.get(), total);
    }

    public void start() {
        intervalCounter = 0;
        totalOk.set(0);
        totalFail.set(0);
        currentBatch.set(0);
        fillBatch();
    }

    public void tick() {
        intervalCounter++;
        if (intervalCounter >= UPDATE_INTERVAL) {
            listener.update(totalOk.get(), totalFail.get(), total);
            fillBatch();
            intervalCounter -= UPDATE_INTERVAL;
        }

        if (done()) {
            listener.finish(totalOk.get(), totalFail.get(), total);
            ((ServerLevelAccess) level).rfx_cmd_pregenerator(null);
        }
    }

    public boolean done() {
        return stop && currentBatch.get() == 0;
    }

    public void stop() {
        stop = true;
    }

    private void fillBatch() {
        int diff = CHUNKS_PER_BATCH - currentBatch.get();
        startBatch(diff);
    }

    private void startBatch(int size) {
        if (!iterator.hasNext()) {
            stop();
        }

        if (stop)
            return;

        CommandsMod.LOGGER.info("Starting pregen batch of {} chunks", size);

        int n = 0;
        List<ChunkPos> list = new ArrayList<>();

        while (iterator.hasNext() && n < size) {
            n++;
            iterator.next();

            list.add(new ChunkPos(iterator.currentX(), iterator.currentZ()));
        }

        currentBatch.addAndGet(list.size());

        for (ChunkPos pos : list) {
            force(pos);
        }

        chunkSrc.tickChunks();

        for (ChunkPos pos : list) {
            ChunkHolder holder = chunkMap.getUpdatingChunkIfPresent(pos.toLong());
            if (holder == null) {
                CommandsMod.LOGGER.error("Chunk ticket not created at [{} {}]", pos.x, pos.z);
                finishChunkGen(ChunkHolder.UNLOADED_CHUNK, pos);
                return;
            }

            holder.getOrScheduleFuture(ChunkStatus.FULL, chunkMap)
                  .whenCompleteAsync((result, err) -> {
                      if (err == null) {
                          result.ifLeft(ch -> {
                              CommandsMod.LOGGER.info("Pregenerated chunk at [{} {}]", pos.x, pos.z);
                          }).ifRight(er -> {
                              CommandsMod.LOGGER.error("Failed generating chunk at [{} {}]: {}", pos.x, pos.z, er);
                          });
                          finishChunkGen(result, pos);
                      } else {
                          CommandsMod.LOGGER.error("Chunk loading threw an exception at [%d %d]".formatted(pos.x, pos.z), err);
                          finishChunkGen(ChunkHolder.UNLOADED_CHUNK, pos);
                      }
                  }, server);
        }
    }

    private void finishChunkGen(Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> result, ChunkPos pos) {
        unforce(pos);

        currentBatch.decrementAndGet();
        result.ifLeft(chunk -> totalOk.getAndIncrement())
              .ifRight(fail -> totalFail.getAndIncrement());
    }

    private void force(ChunkPos pos) {
        chunkSrc.addRegionTicket(TicketType.FORCED, pos, 0, pos);
    }

    private void unforce(ChunkPos pos) {
        chunkSrc.removeRegionTicket(TicketType.FORCED, pos, 0, pos);
    }

    public static Pregenerator get(ServerLevel level) {
        return ((ServerLevelAccess) level).rfx_cmd_pregenerator();
    }

    public static void tick(ServerLevel level) {
        Pregenerator pregen = get(level);
        if (pregen != null)
            pregen.tick();
    }
}
